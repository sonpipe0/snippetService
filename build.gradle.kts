plugins {
	java
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.25.0"
    id("checkstyle")
    kotlin("jvm") version "2.0.21"
}

group = "com.printScript"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
    maven{
        name = "GitHubPackagesAustral"
        url = uri("https://maven.pkg.github.com/austral-ingsis/class-redis-streams")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
	mavenCentral()
}

dependencies {
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.hibernate.validator:hibernate-validator:6.0.13.Final")
    implementation("org.glassfish:javax.el:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    compileOnly("org.projectlombok:lombok")
    implementation("javax.validation:validation-api:2.0.1.Final")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive") // For reactive Redis
    implementation("org.austral.ingsis:redis-streams-mvc:0.1.13")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}

tasks.withType<Test> {
	useJUnitPlatform()
    exclude("**/RedisTests.class")
}

checkstyle {
    toolVersion = "10.18.2"
    configFile = file("config/checkstyle/checkstyle.xml")
}

spotless {
    java {
        googleJavaFormat("1.23.0")
        importOrder("java", "javax", "org", "com", "")
        removeUnusedImports()
        eclipse().configFile("config/eclipse/eclipse-java-formatter.xml")
        target("src/**/*.java")
    }
}

tasks.check {
    dependsOn("checkstyleMain", "checkstyleTest", "spotlessCheck")
}

tasks.build {
    dependsOn("spotlessApply")
}
