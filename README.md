# airline-api-ticarum

Este es un repositorio que aloja el código fuente de una API REST para una aerolínea desarrollada en Java 11 con
Spring Boot v2.5.8 y h2 para la base de datos en el IDE IntelliJ IDEA 2022. La API brinda funcionalidades acerca de los
vuelos de la aerolinea. Además, cuenta con autenticación mediante Token JWT con dos roles "USER" y "ADMIN", y también
está documentada con Swagger.

#### Aspectos a tener en cuenta del proyecto:
* Para ejecutar el proyecto podemos **importar** el proyecto como proyecto *Maven* y ejecutarlo en nuestro IDE o mediante 
  una **terminal** desde el directorio raíz del proyecto teniendo *Maven* configurado correctamente con los comandos 
  *mvn clean install* y después *mvn spring-boot:run*.


* En la clase *GlobalConfig* localizada en el paquete *"com.airline.api.context"* hay 3 **variables** públicas estáticas
  que son **esenciales** para el correcto funcionamiento del proyecto y sobre todo de las pruebas, tenemos:
    * *AIRLINE_NAME:* que es el **nombre** de la aerolínea en minúscula, por defecto es *"airline".*
    * *IS_AUTHENTICATION_ENABLE:* que es un flag para habilitar la **autenticación**, por defecto está **desactivada**.
    * *IS_DATA_INITIALIZATION_ENABLE:* que es un flag para habilitar la **inicialización** de algunos datos de ejemplo
      como la aerolínea, aviones, vuelos y usuarios, por defecto está **desactivada**.

  Dependiendo de nuestros intereses a la hora de probar la aplicación por nuestra cuenta podemos activar la
  autenticación o los datos de ejemplo según nos convenga. Sin embargo, si queremos ejecutar cada una de las
  clases con los **tests unitarios** es **necesario** que los flags estén **desactivados** salvo para la clase test
  *AuthorizedAirlineControllerTest*, en la que ambos flags tienen que estar **activados**, en caso de que no se cumplan
  estas indicaciones los tests fallarán.


* El proyecto utiliza *Lombok* por lo que si utilizamos IntelliJ sólo tenemos que darle a *"Enable annotation
  processing"* que nos sale cuando lo ejecutamos por primera vez o instalar el plugin de *Lombok*. Si estamos utilizando 
  Eclipse en este enlace hay un **manual** de cómo poder instalarlo.
  Enlace: [Instalación de Lombok en Eclipse](https://projectlombok.org/setup/eclipse)


* Si queremos utilizar la **autenticación** con *Swagger*, lo podemos hacer haciendo click en el botón que está en el
  margen superior derecha que pone *"Authorize"*, después escribimos *"Bearer"*, un espacio de separación y a
  continuación escribimos el token JWT que hemos sacado previamente y ya ejecutamos las operaciones que queramos.


* Los usuarios registrados con rol de *"USER"* pueden realizar **sólamente** las operaciones de consulta de datos,
  mientras que los usuarios con rol de *"ADMIN"* pueden realizar todas las operaciones **sin restricciones**.


* A la hora de importar la **colección** de peticiones en *Postman*, ésta se encuentra en el carpeta *"postman"* del
  directorio raíz, también es importante importar el **entorno** o *environment* que se encuentra en la misma carpeta
  ya que las peticiones de la colección dependen de ese *environment*.


* Enlace para acceder a la **documentación** de *Swagger* una vez ejecutado el
  proyecto: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)


* Enlace para acceder a la **consola** de *h2* una vez ejecutado el
  proyecto: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

