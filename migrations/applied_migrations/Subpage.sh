#!/bin/bash

echo "Applying migration Subpage"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /subpage                       controllers.SubpageController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages (English)"
echo "" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "##  Subpage" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "subpage.title = subpage" >> ../conf/messages.en
echo "subpage.heading = subpage" >> ../conf/messages.en

echo "Adding messages to conf.messages (Welsh)"
echo "" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "##  Subpage" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "subpage.title = WELSH NEEDED HERE" >> ../conf/messages.cy
echo "subpage.heading = WELSH NEEDED HERE" >> ../conf/messages.cy

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Subpage completed"
