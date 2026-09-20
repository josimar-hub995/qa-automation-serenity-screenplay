Feature: Validación de servicios API configurados desde Excel

  @TC_API_006 @api @automation @regression @happyPath @xc-ApiDataTest @api_cases
  Scenario Outline: Validar que la API de Reqres liste usuarios correctamente y muestre el contenido esperado con los datos <datos>
    Given se cargan los datos del servicio API identificados como "<datos>"
    When se envía la petición API configurada con los datos "<datos>"
    Then se valida el status code y el response esperados con los datos "<datos>"

    Examples:
      | datos |
      | 1     |

  @TC_API_007 @api @automation @regression @happyPath @xc-ApiDataTest @api_cases
  Scenario Outline: Validar la creación de un nuevo usuario mediante la API de Reqres y el contenido de la respuesta con los datos <datos>
    Given se cargan los datos del servicio API identificados como "<datos>"
    When se envía la petición API configurada con los datos "<datos>"
    Then se valida el status code y el response esperados con los datos "<datos>"

    Examples:
      | datos |
      | 2     |

  @TC_API_008 @api @automation @regression @happyPath @xc-ApiDataTest @api_cases
  Scenario Outline: Validar la actualización de datos de un usuario mediante la API de Reqres y el contenido de la respuesta con los datos <datos>
    Given se cargan los datos del servicio API identificados como "<datos>"
    When se envía la petición API configurada con los datos "<datos>"
    Then se valida el status code y el response esperados con los datos "<datos>"

    Examples:
      | datos |
      | 3     |

  @TC_API_009 @header_error_template @regression @xc-ApiDataTest @api_cases
  Scenario Outline: Validar el comportamiento de Reqres al omitir el header configurado con los datos <datos>
    Given se cargan los datos del servicio API identificados como "<datos>"
    When se envía la petición API configurada con los datos "<datos>"
    Then se valida el status code y el response esperados con los datos "<datos>"

    Examples:
      | datos |
      | 4     |

  @TC_API_010 @header_error_template @regression @xc-ApiDataTest @api_cases
  Scenario Outline: Validar el comportamiento de Reqres al enviar vacío el header configurado con los datos <datos>
    Given se cargan los datos del servicio API identificados como "<datos>"
    When se envía la petición API configurada con los datos "<datos>"
    Then se valida el status code y el response esperados con los datos "<datos>"

    Examples:
      | datos |
      | 5     |

  @TC_API_011 @header_error_template @regression @xc-ApiDataTest @api_cases
  Scenario Outline: Validar el comportamiento de Reqres al enviar incorrecto el header configurado con los datos <datos>
    Given se cargan los datos del servicio API identificados como "<datos>"
    When se envía la petición API configurada con los datos "<datos>"
    Then se valida el status code y el response esperados con los datos "<datos>"

    Examples:
      | datos |
      | 6     |
