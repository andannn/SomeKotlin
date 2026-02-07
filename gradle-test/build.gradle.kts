plugins {
    id("test.plugin")
}

project.createTestTask().configure {
    tag.set("tag is 1.0")
    sourceFile.set(layout.projectDirectory.file("src/main/resources/input.txt"))
    destinationFile.set(layout.buildDirectory.file("generated/output.txt"))
}

dependencies {
}
