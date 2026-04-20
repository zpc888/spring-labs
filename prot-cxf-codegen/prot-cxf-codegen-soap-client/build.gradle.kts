plugins {
    id("java-library") 
}

val cxfVersion = "4.2.0"

dependencies {
    implementation("org.apache.cxf:cxf-codegen-plugin:$cxfVersion")
    implementation("org.apache.cxf:cxf-core:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-bindings-soap:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-wsdl:$cxfVersion")
    implementation(project(":prot-cxf-codegen-plugin"))
}

// Configure CXF wsdl2java code generation
tasks.register<JavaExec>("wsdl2java") {
    group = "build"
    description = "Generate Java code from WSDL using CXF"

    workingDir(projectDir)
    mainClass.set("org.apache.cxf.tools.wsdlto.WSDLToJava")

    args("-d", "src/main/java")
    args("-fe", "sample")
    args("-p", "com.ibm.was.wssample.sei.ping")
    args(file("src/main/resources/ping.wsdl").absolutePath)

    classpath(configurations.runtimeClasspath)
}
