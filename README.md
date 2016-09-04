# JenkinsBot

git clone https://github.com/SymphonyOSF/JenkinsBot.git

cd JenkinsBot

mvn clean package

java 

-Dkeystore.password=keystore_pwd 

-Dtruststore.password=truststore_pwd 

-Dsessionauth.url=https://corporate-api.symphony.com:8444/sessionauth

-Dkeyauth.url=https://corporate-api.symphony.com:8444/keyauth 

-Dpod.url=https://corporate.symphony.com/pod 

-Dagent.url=https://corporate-api.symphony.com:8444/agent

-Dtruststore.file=src/test/resources/server.truststore 

-Dbot.user.name=bot.user20 

-Dbot.user.email=bot.user20@symphony.com

-jar target/symphony-jenkins-bot-1.0-SNAPSHOT-jar-with-dependencies.jar
