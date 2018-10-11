package com.thomas.garrison.emailvalidation

import com.google.gson.annotations.SerializedName

class Email(
        var result: String?,
        var reason: String?,
        var isDisposable: Boolean,
        @field:SerializedName("did_you_mean")
        var didYouMean: String?,
        var email: String?
)