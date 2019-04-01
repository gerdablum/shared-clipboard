package de.alina.clipboard.app.view

interface BaseView {

    fun showLoggedInSuccessful()
    fun showLogoutSuccessful()
    fun showLoginFailure()
    fun showLogoutFailure()
    fun showNoInternetConnection()
    fun showSendDataSuccessful()
    fun showSendDataFailure()
    fun showGetDataSuccessfull()
    fun showGetDataFailure()
    fun showFailure()
}