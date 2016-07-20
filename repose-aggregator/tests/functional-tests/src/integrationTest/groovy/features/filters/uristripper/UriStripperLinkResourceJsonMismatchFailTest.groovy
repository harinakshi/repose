/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */
package features.filters.uristripper

import framework.ReposeValveTest
import groovy.json.JsonBuilder
import org.openrepose.commons.utils.http.media.MimeType
import org.rackspace.deproxy.Deproxy
import org.rackspace.deproxy.Response

import javax.servlet.http.HttpServletResponse

class UriStripperLinkResourceJsonMismatchFailTest extends ReposeValveTest {

    def static String tenantId = "94828347"
    def static responseHeaders = ["Content-Type": MimeType.APPLICATION_JSON.toString()]
    def jsonBuilder

    def setupSpec() {
        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.targetPort)

        def params = properties.getDefaultTemplateParams()
        repose.configurationProvider.applyConfigs("common", params)
        repose.configurationProvider.applyConfigs("features/filters/uristripper/common", params)
        repose.configurationProvider.applyConfigs("features/filters/uristripper/linkresource/json/mismatchfail", params)
        repose.start()
        waitUntilReadyToServiceRequests()
    }

    def setup() {
        jsonBuilder = new JsonBuilder()
    }

    def "when configured to fail on mismatch, Repose returns a 500 if the response body link can't be updated due to no recognizable tokens"() {
        given: "the link in the JSON response doesn't contain the previous nor following token and the alt-link is legit"
        def requestUrl = "/foo/$tenantId/bar"
        jsonBuilder {
            link "/a/b/c/d/e/f/g/h"
            "alt-link" "/a/b/c/d/e/f/g/h"
            "other-field" "some value"
        }
        def responseHandler = { request -> new Response(200, null, responseHeaders, jsonBuilder.toString()) }

        when: "a request is made"
        def mc = deproxy.makeRequest(url: reposeEndpoint + requestUrl, defaultHandler: responseHandler)

        then: "the response code is 500"
        mc.receivedResponse.code == HttpServletResponse.SC_INTERNAL_SERVER_ERROR as String
    }

    def "when configured to fail on mismatch, Repose returns a 500 if the token index is too high for the link"() {
        given: "the alt-link in the JSON response doesn't contain enough tokens and the link is legit"
        def requestUrl = "/foo/$tenantId/bar"
        jsonBuilder {
            link "/foo/bar"
            "alt-link" "/a/b/c"
            "other-field" "some value"
        }
        def responseHandler = { request -> new Response(200, null, responseHeaders, jsonBuilder.toString()) }

        when: "a request is made"
        def mc = deproxy.makeRequest(url: reposeEndpoint + requestUrl, defaultHandler: responseHandler)

        then: "the response code is 500"
        mc.receivedResponse.code == HttpServletResponse.SC_INTERNAL_SERVER_ERROR as String
    }

    def "when configured to fail on mismatch, Repose returns a 500 if the JSON path to the link does not resolve"() {
        given: "the JSON response doesn't contain the link field at all"
        def requestUrl = "/foo/$tenantId/bar"
        jsonBuilder {
            "not-the-link" "/foo/bar"
        }
        def responseHandler = { request -> new Response(200, null, responseHeaders, jsonBuilder.toString()) }

        when: "a request is made"
        def mc = deproxy.makeRequest(url: reposeEndpoint + requestUrl, defaultHandler: responseHandler)

        then: "the response code is 500"
        mc.receivedResponse.code == HttpServletResponse.SC_INTERNAL_SERVER_ERROR as String
    }
}
