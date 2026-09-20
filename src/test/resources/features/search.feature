Feature: Búsqueda de información en Selenium

  @TC_WEB_003 @web @automation @regression @happyPath @xc-DataTest @search
  Scenario Outline: Validar el flujo de búsqueda de WebDriver y los resultados correspondientes con los datos <datos>
    Given que el usuario accede al sitio web de Selenium
    When el usuario realiza una búsqueda usando los datos "<datos>"
    Then deben mostrarse resultados relacionados usando los datos "<datos>"

    Examples:
      | datos |
      | 1     |

  @TC_WEB_004 @web @automation @regression @happyPath @xc-DataTest @search
  Scenario Outline: Validar el flujo de búsqueda de Selenium Grid y los resultados correspondientes con los datos <datos>
    Given que el usuario accede al sitio web de Selenium
    When el usuario realiza una búsqueda usando los datos "<datos>"
    Then deben mostrarse resultados relacionados usando los datos "<datos>"

    Examples:
      | datos |
      | 2     |

  @TC_WEB_005 @web @automation @regression @happyPath @xc-DataTest @search
  Scenario Outline: Validar el flujo de búsqueda de BiDi y los resultados correspondientes con los datos <datos>
    Given que el usuario accede al sitio web de Selenium
    When el usuario realiza una búsqueda usando los datos "<datos>"
    Then deben mostrarse resultados relacionados usando los datos "<datos>"

    Examples:
      | datos |
      | 3     |
