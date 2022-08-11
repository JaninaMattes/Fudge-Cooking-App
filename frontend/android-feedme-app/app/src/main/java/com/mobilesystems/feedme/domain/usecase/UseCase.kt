package com.mobilesystems.feedme.domain.usecase

abstract class UseCase <T> {

    abstract fun executeUseCase(onStatus: (status: T) -> Unit)
}