package com.rackspace.papi.components.keystone.v3.utilities

class KeystoneServiceException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = {
    this(message, null)
  }
}
