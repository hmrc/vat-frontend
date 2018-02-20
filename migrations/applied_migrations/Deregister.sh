#!/bin/bash

echo "Applying migration Deregister"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /deregister                       controllers.DeregisterController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages (English)"
echo "" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "##  Deregister" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "deregister.title = deregister" >> ../conf/messages.en
echo "deregister.heading = deregister" >> ../conf/messages.en

echo "Adding messages to conf.messages (Welsh)"
echo "" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "##  Deregister" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "deregister.title = WELSH NEEDED HERE" >> ../conf/messages.cy
echo "deregister.heading = WELSH NEEDED HERE" >> ../conf/messages.cy

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Deregister completed"
