For the time being, smartsprites is not deployed in maven.
As Jawr use maven, you will need to download it manually and install it locally.
As I'm writing the latest version is : 0.3.0-dev

Please download this version from : 
http://download.csssprites.org/  

Extract the jar file /smartsprites/lib/smartsprites-0.3.0-dev.jar under /{jawr}/smartsprites/mvn

Then go /{jawr}/smartsprites/mvn and launch the command :

mvn install:install-file -Dfile=smartsprites-0.3.0-dev.jar -DpomFile=spmartsprites-pom.xml

It's done. Smartsprites is now installed on you local repository.
You can now use maven to build Jawr.