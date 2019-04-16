#!/bin/bash

#  The retrieval does work, however, the namespaces are wrong for the purpose of creating JAXB out of them,
#  which is corrected wtihin the groovy class that calls this.  KLS

echo "    Retrieving Marking Extension schemas for version ${1}"
cd src/main/resources/schemas/v${1}

# these schemas are extensions to the data markings, used to communicate with DHS's Flare Hub
mkdir marking_extensions
cd marking_extensions
# AIS
curl -O https://www.us-cert.gov/sites/default/files/STIX_Namespace/AIS_Bundle_Marking_1.1.1_v1.0.xsd