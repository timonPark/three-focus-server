package com.threefocus.global.exception

class ApiException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
