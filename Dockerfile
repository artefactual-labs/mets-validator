FROM maven:latest
COPY . /mets-validator
WORKDIR /mets-validator
RUN mvn package -Dmaven.test.skip=true
WORKDIR /aip
ENTRYPOINT ["java", "-jar", "/mets-validator/target/mets-validator-1.0-jar-with-dependencies.jar"]
