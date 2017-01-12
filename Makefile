CONTAINER_NAME=gocd-server
ifeq ($(OS),Windows_NT)
    GRADLE=gradlew.bat
else
    GRADLE=./gradlew
endif

start-vm:
	vagrant up

deploy:
	$(GRADLE) clean test assemble
	vagrant ssh -c "sudo service go-server restart"

