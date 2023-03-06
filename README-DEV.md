# Для разработчика

## Зависимости

Генерация дерева зависимостей https://maven.apache.org/plugins/maven-dependency-plugin/index.html

```shell
mvn dependency:tree -DoutputFile=./maven-dependency-tree.gv -DoutputType=dot
```

Просмотр http://webgraphviz.com/

# Настройка логирования

```
-DLOGGER_NAME=ru.ezhov
-DLOGBACK_LOGGER_LEVEL=INFO
```
