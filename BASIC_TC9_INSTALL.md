###Installation guide for XTR component full installation from the scratch to debian based linux. 

*Note!* Ignore this if you are using Docker to deploy and follow [README](README.md#deploy-with-docker) instructions instead.

1.**Update dependencies**
```
sudo apt update -y
```
2.**Install Git**
```
sudo apt install git -y
```

3.**Install wget**
```
sudo apt install wget -y
```
4.**Install JDK**
```
sudo apt install default-jdk -y
```

5.**Install Maven**
```
sudo apt install maven -y
```

6.**Install curl**
```
sudo apt install curl -y
```

7.**Set up maven Nexus repository**
```
mkdir -p ~/.m2/ && echo "<settings><mirrors><mirror><id>nexus</id><mirrorOf>external:*</mirrorOf><url>https://example.com/maven-public/</url></mirror></mirrors></settings>" > ~/.m2/settings.xml
```

8.**Get Tomcat 9**
```
cd /tmp && wget https://downloads.apache.org/tomcat/tomcat-9/v9.0.35/bin/apache-tomcat-9.0.35.tar.gz
```

9.**Unpack Tomcat 9 archive**
```
tar xzf apache-tomcat-9.0.35.tar.gz
sudo mv apache-tomcat-9.0.35 /var/lib/tomcat9 && rm -rf ./apache-tomcat-9.0.35.tar.gz
```

10.**Create environment variables requred by Tomcat**
```
echo "export CATALINA_HOME="/var/lib/tomcat9"" >> ~/.bashrc
echo "export JAVA_HOME="/usr/lib/jvm/default-java"" >> ~/.bashrc
echo "export JRE_HOME="/usr/lib/jvm/default-java"" >> ~/.bashrc
source ~/.bashrc
```

11.**Create directory for XTR services**
```
mkdir -p /var/lib/tomcat9/services
```

12.**Set permissions**
```
sudo chgrp -R tomcat /var/lib/tomcat9/
cd /var/lib/tomcat9/
chmod -R g+r conf
chmod g+x conf
chown -R tomcat webapps/ work/ temp/ logs/ services/
chmod +x ./bin/startup.sh
```

13.**Get XTR source code and compile it**
```
cd /tmp
git clone https://stash.ria.ee/scm/rig/ee.eesti.java.xtr.git
cd ./ee.eesti.java.xtr && mvn clean -U package && mvn clean -U package -Pservices
```

14.**Move the configurations**
```
cp xtee-samples/target/xtee-samples-*/* /var/lib/tomcat9/services/
```

15.**Edit Tomcats context.xml to load the services from services directory**
```
vi /var/lib/tomcat9/conf/context.xml

    <Resources>
        <PostResources className="org.apache.catalina.webresources.DirResourceSet"
                       base="${catalina.base}/services" webAppMount="/WEB-INF/lib"/>
    </Resources>

```

16.**Start Tomcat** 
```
/var/lib/tomcat9/bin/startup.sh
```

17.**Check if XTR is up&running**
```
curl localhost:8080/xtr/healthz
```
