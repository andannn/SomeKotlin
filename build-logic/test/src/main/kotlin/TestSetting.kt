import org.gradle.api.Named
import javax.inject.Inject

abstract class TestSetting
    @Inject
    constructor(
        private val name: String,
    ) : Named {
        override fun getName() = name

        // 使用 var 或 Property 定义可配置的属性
        var url: String = ""
        var debug: Boolean = false
    }
