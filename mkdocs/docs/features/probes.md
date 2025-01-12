# Пробы приложения

При подключении модуля `ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule` у приложения появляются два метода для получения проб на приватном порту (по умолчанию 8085)

## Liveness probe

Эта проба отвечает за признак - является ли приложение живым в данный момент. Kora старается начать отдавать эту пробу как можно раньше, чтобы оркестраторы точно знали, что нет проблем при старте и не пытались сделать рестарт приложения.

По умолчанию она доступна по пути `/system/liveness`, но её можно настроить через конфигурацию

```hocon
httpServer {
    privateApiHttpLivenessPath = "/liveness"
}
```

## Readiness probe

Эта проба отвечает за признак - является ли приложение живым в данный момент. Kora старается начать отдавать эту пробу как можно раньше, чтобы оркестраторы точно знали, что нет проблем при старте и не пытались сделать рестарт приложения.

По умолчанию она доступна по пути `/system/readiness`, но её можно настроить через конфигурацию

```hocon
httpServer {
    privateApiHttpReadinessPath = "/readiness"
}
```

## Создание своих проб

Для того чтобы добавить свою пробу, достаточно в контейнер добавить свою реализацию класса `ru.tinkoff.kora.common.liveness.LivenessProbe` или `ru.tinkoff.kora.common.readiness.ReadinessProbe`


**!! Мы крайне не рекомендуем делать пробы, которые проверяют внешние зависимости, например базы данных или другие сервисы !!**  
В случае недоступности каких то внешних зависимостей рекомендуется использовать circuit breaker pattern  

Хорошим примером для readiness probe может служить проба, которая возвращает ошибку во время прогрева сервиса


  
