/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.algorand.android

import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.algorand.android.banner.domain.usecase.BannersUseCase
import com.algorand.android.core.BaseViewModel
import com.algorand.android.database.NodeDao
import com.algorand.android.deviceregistration.domain.usecase.DeviceIdMigrationUseCase
import com.algorand.android.deviceregistration.domain.usecase.DeviceIdUseCase
import com.algorand.android.deviceregistration.domain.usecase.DeviceRegistrationUseCase
import com.algorand.android.deviceregistration.domain.usecase.FirebasePushTokenUseCase
import com.algorand.android.deviceregistration.domain.usecase.UpdatePushTokenUseCase
import com.algorand.android.models.AnnotatedString
import com.algorand.android.models.AssetOperationResult
import com.algorand.android.models.Node
import com.algorand.android.models.SignedTransactionDetail
import com.algorand.android.modules.deeplink.ui.DeeplinkHandler
import com.algorand.android.modules.tracking.bottomnavigation.BottomNavigationEventTracker
import com.algorand.android.modules.tracking.moonpay.MoonpayEventTracker
import com.algorand.android.network.AlgodInterceptor
import com.algorand.android.network.IndexerInterceptor
import com.algorand.android.network.MobileHeaderInterceptor
import com.algorand.android.tutorialdialog.domain.usecase.AccountAddressTutorialDisplayPreferencesUseCase
import com.algorand.android.usecase.SendSignedTransactionUseCase
import com.algorand.android.utils.AccountCacheManager
import com.algorand.android.utils.AssetName
import com.algorand.android.utils.AutoLockManager
import com.algorand.android.utils.DataResource
import com.algorand.android.utils.Event
import com.algorand.android.utils.Resource
import com.algorand.android.utils.coremanager.BlockPollingManager
import com.algorand.android.utils.exception.AccountAlreadyOptedIntoAssetException
import com.algorand.android.utils.findAllNodes
import com.algorand.android.utils.sendErrorLog
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
@HiltViewModel
class MainViewModel @Inject constructor(
    private val autoLockManager: AutoLockManager,
    private val sharedPref: SharedPreferences,
    private val nodeDao: NodeDao,
    private val indexerInterceptor: IndexerInterceptor,
    private val mobileHeaderInterceptor: MobileHeaderInterceptor,
    private val algodInterceptor: AlgodInterceptor,
    private val accountCacheManager: AccountCacheManager,
    private val blockPollingManager: BlockPollingManager,
    private val bannersUseCase: BannersUseCase,
    private val deviceRegistrationUseCase: DeviceRegistrationUseCase,
    private val deviceIdMigrationUseCase: DeviceIdMigrationUseCase,
    private val firebasePushTokenUseCase: FirebasePushTokenUseCase,
    private val updatePushTokenUseCase: UpdatePushTokenUseCase,
    private val deviceIdUseCase: DeviceIdUseCase,
    private val bottomNavigationEventTracker: BottomNavigationEventTracker,
    private val deepLinkHandler: DeeplinkHandler,
    private val moonpayEventTracker: MoonpayEventTracker,
    private val accountAddressTutorialDisplayPreferencesUseCase: AccountAddressTutorialDisplayPreferencesUseCase,
    private val sendSignedTransactionUseCase: SendSignedTransactionUseCase
) : BaseViewModel() {

    // TODO: Replace this with Flow whenever have time
    val assetOperationResultLiveData = MutableLiveData<Event<Resource<AssetOperationResult>>>()

    private val _shouldShowAccountAddressCopyTutorialDialogFlow = MutableStateFlow(false)
    val shouldShowAccountAddressCopyTutorialDialogFlow: StateFlow<Boolean>
        get() = _shouldShowAccountAddressCopyTutorialDialogFlow

    // TODO I'll change after checking usage of flow in activity.
    val accountBalanceSyncStatus = accountCacheManager.getCacheStatusFlow().asLiveData()

    val autoLockLiveData
        get() = autoLockManager.autoLockLiveData

    fun checkLockState() {
        autoLockManager.checkLockState()
    }

    private var sendTransactionJob: Job? = null
    var refreshBalanceJob: Job? = null
    var registerDeviceJob: Job? = null

    init {
        initializeAccountCacheManager()
        initializeNodeInterceptor()
        observeFirebasePushToken()
        refreshFirebasePushToken(null)
    }

    private fun observeFirebasePushToken() {
        viewModelScope.launch {
            firebasePushTokenUseCase.getPushTokenCacheFlow().collect {
                if (it?.data.isNullOrBlank().not()) registerFirebasePushToken(it?.data.orEmpty())
            }
        }
    }

    private fun initializeNodeInterceptor() {
        viewModelScope.launch(Dispatchers.IO) {
            if (indexerInterceptor.currentActiveNode == null) {
                val lastActivatedNode = findAllNodes(sharedPref, nodeDao).find { it.isActive }
                lastActivatedNode?.activate(indexerInterceptor, mobileHeaderInterceptor, algodInterceptor)
            }
            migrateDeviceIdIfNeed()
        }
    }

    private fun registerFirebasePushToken(token: String) {
        registerDeviceJob?.cancel()
        registerDeviceJob = viewModelScope.launch(Dispatchers.IO) {
            deviceRegistrationUseCase.registerDevice(token).collect {
                if (it is DataResource.Success) bannersUseCase.initializeBanner(deviceId = it.data)
            }
        }
    }

    fun refreshFirebasePushToken(previousNode: Node?) {
        if (previousNode != null) deletePreviousNodePushToken(previousNode)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            firebasePushTokenUseCase.setPushToken(token)
        }
    }

    private fun deletePreviousNodePushToken(previousNode: Node) {
        viewModelScope.launch(Dispatchers.IO) {
            val deviceId = deviceIdUseCase.getNodeDeviceId(previousNode) ?: run {
                return@launch
            }
            updatePushTokenUseCase.updatePushToken(deviceId, null, previousNode.networkSlug).collect()
        }
    }

    private suspend fun migrateDeviceIdIfNeed() {
        deviceIdMigrationUseCase.migrateDeviceIdIfNeed()
    }

    private fun initializeAccountCacheManager() {
        viewModelScope.launch(Dispatchers.IO) {
            accountCacheManager.initializeAccountCacheMap()
        }
    }

    fun resetBlockPolling() {
        refreshBalanceJob?.cancel()
        blockPollingManager.startJob()
    }

    fun sendAssetOperationSignedTransaction(transaction: SignedTransactionDetail.AssetOperation) {
        if (sendTransactionJob?.isActive == true) {
            return
        }

        sendTransactionJob = viewModelScope.launch(Dispatchers.IO) {
            sendSignedTransactionUseCase.sendSignedTransaction(transaction).collectLatest { dataResource ->
                when (dataResource) {
                    is DataResource.Success -> {
                        val assetActionResult = getAssetOperationResult(transaction)
                        assetOperationResultLiveData.postValue(Event(Resource.Success(assetActionResult)))
                    }
                    is DataResource.Error.Api -> {
                        assetOperationResultLiveData.postValue(Event(Resource.Error.Api(dataResource.exception)))
                    }
                    is DataResource.Error.Local -> {
                        val errorResourceId = if (dataResource.exception is AccountAlreadyOptedIntoAssetException) {
                            R.string.you_are_already
                        } else {
                            R.string.an_error_occured
                        }
                        val assetName = transaction.assetInformation.fullName.toString()
                        assetOperationResultLiveData.postValue(
                            Event(
                                Resource.Error.GlobalWarning(
                                    titleRes = R.string.error,
                                    annotatedString = AnnotatedString(
                                        stringResId = errorResourceId,
                                        replacementList = listOf("asset_name" to assetName)
                                    )
                                )
                            )
                        )
                    }
                    else -> {
                        sendErrorLog("Unhandled else case in MainViewModel.sendSignedTransaction")
                    }
                }
            }
        }
    }

    fun handleDeepLink(uri: String) {
        deepLinkHandler.handleDeepLink(uri)
    }

    fun setDeepLinkHandlerListener(listener: DeeplinkHandler.Listener) {
        deepLinkHandler.setListener(listener)
    }

    fun setupAutoLockManager(lifecycle: Lifecycle) {
        autoLockManager.registerAppLifecycle(lifecycle)
    }

    fun logBottomNavAlgoPriceTapEvent() {
        viewModelScope.launch {
            bottomNavigationEventTracker.logAlgoPriceTapEvent()
        }
    }

    fun logBottomNavAccountsTapEvent() {
        viewModelScope.launch {
            bottomNavigationEventTracker.logAccountsTapEvent()
        }
    }

    fun logBottomNavigationBuyAlgoEvent() {
        viewModelScope.launch {
            bottomNavigationEventTracker.logBottomNavigationAlgoBuyTapEvent()
        }
    }

    fun logMoonpayAlgoBuyCompletedEvent() {
        viewModelScope.launch {
            moonpayEventTracker.logMoonpayAlgoBuyCompletedEvent()
        }
    }

    fun checkAccountAddressCopyTutorialDialogState() {
        viewModelScope.launch {
            _shouldShowAccountAddressCopyTutorialDialogFlow.value =
                accountAddressTutorialDisplayPreferencesUseCase.shouldShowDialogByApplicationOpeningCount()
        }
    }

    private fun getAssetOperationResult(transaction: SignedTransactionDetail.AssetOperation): AssetOperationResult {
        val assetName = transaction.assetInformation.fullName ?: transaction.assetInformation.shortName
        val resultTitleResId = when (transaction) {
            is SignedTransactionDetail.AssetOperation.AssetAddition -> R.string.asset_successfully_added_to_your
            is SignedTransactionDetail.AssetOperation.AssetRemoval -> R.string.asset_successfully_removed_from_your
        }
        return AssetOperationResult(
            resultTitleResId = resultTitleResId,
            assetName = AssetName.create(assetName)
        )
    }
}
