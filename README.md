# XTR application

The purpose of this application is to provide an json api interface on top of x-tee services. The services in wsdl format 
can be found and added at the time of writing at https://www.riha.ee. The wsdl files are used to generate yaml configuration 
files as well as jar packages used by the given application to provide the services.

### Deploy with Docker

To run application using Docker run:

```docker-compose up -d```

*Note!* First deployment may take 5-15 minutes, depending on your computer. This is due to the amount of samples provided.

### Building the project

Project can be compiled and packaged with the command below. 

```
mvn clean -U package
```

After compilation, deployable war container can be found under ./xtr/targer directory.

### Generating configuration via maven

In order to build the configurations via maven add the wsdl files to ./xtee-samples/wsdl folder. NB! Note that each
wsdl file should be in its own separate subdirecotry, like for example  ./xtee-samples/wsdl/my-service.

After including the wsdl files run the command below.

```
mvn clean -U package -Pservices
```

After the script has finished the required configuration files should be available at ./xtee-samples/target directory.

### Generating configuration via automated script `generate.sh`

The generate.sh shell script can be found under xgen module. It needs two parameters - the location of the wsdl file(s)
and directory to output to. Example provided below.

Execution: `./generate.sh WSDL_DIR OUTPUT_DIR`

* WSDL_DIR - Directory of wsdl files. NB! Every wsdl file must be located in its own subdirectory.
* JAR_DIR - Directory where generated jar and configuration yaml files are output to.

Script is looking for subdirectories at ```WSDL_DIR``` directory and generates a single jar package per subdirectory.


### Example of generated yaml configuration from service wsdl 

 _adit_getUserInfo.yaml_
 
```yaml
method: getUserInfoV2
namespaceUri: http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid
operationName: getUserInfo
registryCode: adit
responseRootNode: null
serviceCode: getUserInfo
serviceName: aditXRoadDatabase
version: v2
attachments:
  request:
    type: LIST_TO_XML
    header: <?xml version="1.0" encoding="utf-8"?><getUserInfoRequestAttachmentV1><user_list>
    element: <code>%s</code>
    footer: </user_list></getUserInfoRequestAttachmentV1>
    listProperty: code
``` 

### Dev config
##### Better logging
For your own sanity, set spring WS logging level to TRACE. This way you can see the whole XML sent to xroad security server.
```yaml
logging.level.org.springframework.ws: TRACE
``` 
 
### Configuring attachments

Configuration of attachments of the services, if there are any present, can not be automized, therefore they must be 
configured manually.

#### Attachment compilers

General configurations:
* content - Value type is an array. Possible values cam include:
    * BASE64 - Attachment is encoded in base64 
    * GZIP - Attachment is archived with gzip

##### LIST_TO_XML 

Assembles an xml file from given array values.

Configuration:
* header - file header template
* footer - file footer template
* element - element template (f.e. \<code>%s\</code> - %s)
* listProperty - name of the array

###### Example

Request:

```json
{  
   "register":"abcd",
   "service":"efgh",
   "stripCountryPrefix":[  
      "callerPersonlCode"
   ],
   "parameters":{  
      "callerPersonalCode":"0000000001",
      "code":[  
         "0000000002",
         "EE0000000003",
         "EE0000000004"
      ]
   }
}
```

Attachment configuration:

```yaml
request:
    type: LIST_TO_XML
    header: <?xml version="1.0" encoding="utf-8"?><list>
    element: <code>%s</code>
    footer: </list>
    listProperty: code
    content: [BASE64, GZIP]
```

Result:

```xml
<?xml version="1.0" encoding="utf-8"?>
<list>
	<code>0000000002</code>
	<code>EE0000000003</code>
	<code>EE0000000004</code>
</list>
```

##### JSON_TO_XML 

Assembles an xml file from json request. Personal identity number (callerPersonalCode, teostajaIsikukood, isikukood) 
are no included into xml file. 

Configuration:
* rootElement - Root element of the xml file being assembled
* nestedArrays - Comma separated values in in form of foo#bar where _foo_ is the name of the array and _bar_ is the name 
of the element.
* ignoreFields - List of json field to be excluded from the xml file.

###### Example 

Request:

```json
{  
   "callerPersonalCode":"0000000001",
   "document":{  
      "title":"avaldus",
      "content":"this is content",
      "files":[  
         {  
            "name":"avaldus",
            "content_type":"text/plain",
            "data":"SvVnZXZ"
         }
      ]
   }
}
```

Attachment configuration:

```yaml
request:
    type: JSON_TO_XML
    rootElement: root_element
    nestedArrays: files#file
    ignoreFields: [callerPersonalCode,isikukood]
```

Result:

```xml
<root_element>
  <document>
    <title>avaldus</title>
    <content>this is content</content>
    <files> <!-- nestedArrays: files#file -->
      <file>
        <name>avaldus</name>
        <content_type>text/plain</content_type>
        <data>SvVnZXZ</data>
      </file>
    </files>
  </document>
</root_element>
```

#### Reading attachments from response

Configuration:

* attachmentRootElement - root element of the attachment
* href - path result of the href value separated by '#' for example in case of ```<keha><document href="asdasdad"/></keha>``` 
  href value is ```keha#document ```
* decodeBase64 - Base64 decode response attachment. The value is ```false``` by default.
