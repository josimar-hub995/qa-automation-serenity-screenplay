Feature: Validación de la página de inicio de Selenium

  @TC_WEB_001 @web @automation @regression @happyPath @xc-DataTest @home
  Scenario Outline: Validar que la página de inicio cargue correctamente y muestre el título esperado con los datos <datos>
    Given que el usuario accede al sitio web de Selenium
    Then la página de inicio debe mostrarse correctamente usando los datos "<datos>"

    Examples:
      | datos |
      | 1     |
