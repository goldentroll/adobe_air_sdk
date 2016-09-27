package com.adjust.sdk {
    public class AdjustAttribution {
        private var trackerToken:String;
        private var trackerName:String;
        private var campaign:String;
        private var network:String;
        private var creative:String;
        private var adgroup:String;
        private var clickLabel:String;

        public function AdjustAttribution(trackerToken:String,
            trackerName:String,
            campaign:String,
            network:String,
            creative:String,
            adgroup:String,
            clickLabel:String) {
            this.trackerToken = trackerToken == null ? "" : trackerToken;
            this.trackerName = trackerName == null ? "" : trackerName;
            this.campaign = campaign == null ? "" : campaign;
            this.network = network == null ? "" : network;
            this.creative = creative == null ? "" : creative;
            this.adgroup = adgroup == null ? "" : adgroup;
            this.clickLabel = clickLabel == null ? "" : clickLabel;
        }

        // Getters.
        public function getTrackerToken():String {
            return this.trackerToken;
        }

        public function getTrackerName():String {
            return this.trackerName;
        }

        public function getCampaign():String {
            return this.campaign;
        }

        public function getNetwork():String {
            return this.network;
        }

        public function getCreative():String {
            return this.creative;
        }

        public function getAdGroup():String {
            return this.adgroup;
        }

        public function getClickLabel():String {
            return this.clickLabel;
        }
    }
}
