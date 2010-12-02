@artifact.package@import com.ulcjava.base.shared.CoderRegistry
import com.canoo.grails.ulc.server.ULCApplicationHolder

class CoderRegistryHolder {
    static CoderRegistry getCoderRegistry() {
        return ULCApplicationHolder.getCoderRegistry('@application.alias@')
    }
}
