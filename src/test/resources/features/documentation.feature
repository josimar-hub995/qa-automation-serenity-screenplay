Feature: Navegación a la documentación de Selenium

  @TC_WEB_002 @web @automation @regression @happyPath @xc-DataTest @documentation
  Scenario Outline: Validar la navegación a Documentation y la carga de la página esperada con los datos <datos>
    Given que el usuario accede al sitio web de Selenium
    When el usuario abre la documentación usando los datos "<datos>"
    Then la página de documentación esperada debe mostrarse usando los datos "<datos>"

    Examples:
      | datos |
      | 1     |
