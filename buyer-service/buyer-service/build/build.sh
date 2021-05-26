JAVA_HOME="/usr/lib/jvm/default-java"
export JAVA_HOME

rm -r tmp/
mkdir tmp/

cd ../

./mvnw clean install

cp target/buyer-service-0.0.1-SNAPSHOT.jar build/tmp/
