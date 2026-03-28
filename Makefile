.PHONY: crew run run-docker clean build e2e

crew:
	docker compose up -d postgres

run: crew
	./gradlew :app:bootRun

run-docker:
	docker compose up -d

clean:
	./gradlew clean
	docker compose down --volumes --remove-orphans

build:
	./gradlew :app:build :admin-hub:build :e2e:assemble
	docker build -t service-auth-app ./app

e2e:
	$(MAKE) build
	docker compose up -d
	@echo "Waiting for app to become healthy..."
	@until curl -sf http://localhost:8080/actuator/health 2>/dev/null | grep -q '"status":"UP"'; do \
		sleep 2; \
	done
	./gradlew :e2e:test; status=$$?; docker compose down; exit $$status
