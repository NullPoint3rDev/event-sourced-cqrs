plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.4"
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
    }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
