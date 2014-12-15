package features.filters.addheader

import framework.ReposeValveTest
import org.rackspace.deproxy.Deproxy
import org.rackspace.deproxy.MessageChain
import org.rackspace.deproxy.Response

/**
 * Created by jennyvo on 12/15/14.
 */
class AddHeaderOverwriteTest extends ReposeValveTest {

    def setupSpec() {
        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.targetPort)

        def params = properties.defaultTemplateParams
        repose.configurationProvider.applyConfigs("common", params)
        repose.configurationProvider.applyConfigs("features/filters/addheader", params)
        repose.configurationProvider.applyConfigs("features/filters/addheader/overwritetrue", params)
        repose.start()
    }

    def cleanupSpec() {
        deproxy.shutdown()
        repose.stop()
    }

    def "When using add-header filter the expect header(s) in config is added to request/response" () {
        given:
        def Map headers = ["x-rax-user": "test-user", "x-rax-groups": "reposegroup1", "repose-test": "no-overwrite", "overwrite-test": "will-be-overwrite"]

        when: "Request contains value(s) of the target header"
        def mc = deproxy.makeRequest([url: reposeEndpoint, headers: headers])
        def reposehandling = ((MessageChain) mc).getHandlings()[0]

        then: "The request/response should contain additional header from add-header config"
        reposehandling.request.headers.contains("x-rax-user")
        reposehandling.request.headers.getFirstValue("x-rax-user") == "test-user"
        reposehandling.request.headers.contains("x-rax-groups")
        reposehandling.request.headers.getFirstValue("x-rax-groups") == "reposegroup1"
        reposehandling.request.headers.contains("repose-test")
        reposehandling.request.headers.getFirstValue("repose-test") == "no-overwrite"
        reposehandling.request.headers.contains("overwrite-test")
        reposehandling.request.headers.getFirstValue("overwrite-test") == "this-is-overwrite-value;q=0.5"
        reposehandling.response.headers.contains("response-header")
        reposehandling.response.headers.getFirstValue("response-header") == "foooo"
    }

    def "Add-header filter test with overwrite and quality" () {
        given:
        def Map headers = ["x-rax-user": "test-user", "x-rax-groups": "reposegroup1","overwrite-quality-test": "not-overwrite;q=0.5"]

        when: "Request contains value(s) of the target header"
        def mc = deproxy.makeRequest([url: reposeEndpoint, headers: headers])
        def reposehandling = ((MessageChain) mc).getHandlings()[0]

        then: "The request/response should contain additional header from add-header config"
        reposehandling.request.headers.contains("x-rax-user")
        reposehandling.request.headers.getFirstValue("x-rax-user") == "test-user"
        reposehandling.request.headers.contains("x-rax-groups")
        reposehandling.request.headers.getFirstValue("x-rax-groups") == "reposegroup1"
        reposehandling.request.headers.contains("repose-test")
        reposehandling.request.headers.getFirstValue("repose-test") == "this-is-a-test;q=0.5"
        reposehandling.request.headers.contains("overwrite-test")
        reposehandling.request.headers.getFirstValue("overwrite-test") == "this-is-overwrite-value;q=0.5"
        reposehandling.request.headers.contains("overwrite-quality-test")
        reposehandling.request.headers.getFirstValue("overwrite-quality-test") == "this-is-overwrite-value;q=0.2"
    }

    def "When add response header with overwrite true" () {
        given:
        def Map headers = ["response-header": "will-be-overwrite;q=0.5"]
        def customeHandler = {return new Response(200, "OK", headers, "this is add header test")}

        when: "Request contains value(s) of the target header"
        def mc = deproxy.makeRequest([url: reposeEndpoint, headers: headers, defaultHandler: customeHandler])

        then:
        mc.receivedResponse.headers.contains("response-header")
        mc.receivedResponse.headers.getFirstValue("response-header") == "foooo"

    }

}
