#!/bin/bash

echo "Applying migration Partial"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /partial                       controllers.PartialController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages (English)"
echo "" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "##  Partial" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "partial.title = partial" >> ../conf/messages.en
echo "partial.heading = partial" >> ../conf/messages.en

echo "Adding messages to conf.messages (Welsh)"
echo "" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "##  Partial" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "partial.title = WELSH NEEDED HERE" >> ../conf/messages.cy
echo "partial.heading = WELSH NEEDED HERE" >> ../conf/messages.cy

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Partial completed"
