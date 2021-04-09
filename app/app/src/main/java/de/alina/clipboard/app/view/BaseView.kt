package de.alina.clipboard.app.view

interface BaseView {

    fun showLoggedInSuccessful()
    fun showLogoutSuccessful()
    fun showLoginFailure()
    fun showLogoutFailure()
    fun showNoInternetConnection()
    fun showSendDataSuccessful()
    fun showSendDataFailure()
    fun showGetDataSuccessful()
    fun showGetDataFailure()
    fun showFailure()
    fun serverUrlIncorrect()
}