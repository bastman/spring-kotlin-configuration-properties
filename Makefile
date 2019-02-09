GRADLE_VERSION=4.10.3

print-%: ; @echo $*=$($*)
guard-%:
	@test ${${*}} || (echo "FAILED! Environment variable $* not set " && exit 1)
	@echo "-> use env var $* = ${${*}}";

.PHONY : help
help : Makefile
	@sed -n 's/^##//p' $<


## idea-start:   : start intellij
idea-start:
	open -a /Applications/IntelliJ\ IDEA.app

## gradle-wrapper:   : install gradle wrapper
gradle-wrapper:
	./gradlew --version
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION)
	./gradlew --version

## boot-run.dev:   : run spring-boot (spring.profiles.active=dev)
boot-run.dev:
	./gradlew bootRun --args='--spring.profiles.active=dev'
## boot-run.prod:   : run spring-boot (spring.profiles.active=prod)
boot-run.prod:
	./gradlew bootRun --args='--spring.profiles.active=prod'