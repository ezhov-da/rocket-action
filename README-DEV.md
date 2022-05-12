# Для разработчика

## Зависимости
Генерация дерева зависимостей https://maven.apache.org/plugins/maven-dependency-plugin/index.html
```shell
mvn dependency:tree -DoutputFile=./maven-dependency-tree.gv -DoutputType=dot
```

Просмотр http://webgraphviz.com/

## Ссылки на документацию используемых инструментов

### H2DB
- http://www.h2database.com/html/datatypes.html

### Liquibase
- https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html
- https://docs.liquibase.com/tools-integrations/maven/using-liquibase-and-maven-pom-file.html
- https://docs.liquibase.com/tools-integrations/maven/workflows/creating-liquibase-projects-with-maven-postgresql.html
- https://habr.com/ru/post/436994/
- https://www.liquibase.com/blog/adding-liquibase-on-an-existing-project
- https://www.liquibase.com/blog/3-ways-to-run-liquibase

### Arrow Kt
- https://arrow-kt.io/docs/apidocs/arrow-core/arrow.core/-either/

### ktorm.org
- https://www.ktorm.org/en/quick-start.html
- https://www.ktorm.org/en/transaction-management.html
- https://www.ktorm.org/en/schema-definition.html
- https://www.ktorm.org/en/query.html
- https://habr.com/ru/post/414483/

### AssertJ
- https://joel-costigliola.github.io/assertj/assertj-core-quick-start.html
