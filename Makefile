CONTAINER_NAME=gocd-server
ifeq ($(OS),Windows_NT)
    GRADLE=gradlew.bat
else
    GRADLE=./gradlew
endif
include gradle.properties

start-vm:
	vagrant up

deploy:
	$(GRADLE) clean test assemble
	vagrant ssh -c "sudo rm -f /var/lib/go-server/plugins/external/*.jar"
	vagrant ssh -c "sudo cp \"/vagrant/build/libs/hipchat-plugin-$(version).jar\" \"/var/lib/go-server/plugins/external/hipchat-plugin.jar\""
	vagrant ssh -c "sudo service go-server restart"

