CONTAINER_NAME=gocd-server
ifeq ($(OS),Windows_NT)
    GRADLE=gradlew.bat
else
    GRADLE=./gradlew
endif

run-container: clean-container
	docker run -p 8153:8080 --name $(CONTAINER_NAME) -tiP gocd/gocd-dev

clean-container:
	docker rm $(CONTAINER_NAME) || true

assemble:
	$(GRADLE) clean test assemble


deploy: assemble
	docker exec -i $(CONTAINER_NAME) sh -c 'OUTFILE=/var/lib/go-server/plugins/external/hipchat-plugin.jar; cat >/tmp/temp.go.plugin; chown go:go /tmp/temp.go.plugin; mv /tmp/temp.go.plugin "$$OUTFILE"'< build/libs/hipchat-plugin-0.1-SNAPSHOT.jar

