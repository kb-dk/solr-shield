# Solr Shield

Acts as a gateway between one or more SolrClouds and the open Internet. Intended for exposing Solr functionality as part of an open API or similar. Focus is on extensible user-controlled vetting of the queries, guarding (or at least attempting to) against denial of service queries like excessive grouping, regexp bombs or function queries.

## Requirements

* Java 11
* Tomcat 9
* A Solr 8 installation

## Status

Extremely preliminary. Just notes (in this README) at the moment.

## Principle

Solr Shield uses a series of rules, coupled to groups. A request is always given with one or more groups, where the most permissive group takes precedence. The individual rules consists of a Solr parameter, constraints for the parameter and actions if the constraints are exceeded.

# Collections definition

## Argument definition

Argument are always coupled to a Solr parameter key. It has a list of rules, which are iterated in order. If a rule matches the input, it is activated and no further rules are processed.

Rules can cancel processing fully or they can adjust the cost of the query. If the query exceeds the cost, processing is cancelled. Costs are additive across collections.

- key string (mandatory)
- collections comma-separated-strings
- baseCost double
- maxAllowedCost double
- costExceededMessage string
- default value

## Rule definition

Actions are the same for the different types of rules

- matchAction [cancel setValue]
- matchAddition double
- matchMultiplier double
- matchMessage string

- isDefined bool
- continue: bool
- equals value

### integer

- lessThan int
- greaterThan int

## Elements

One or more running SolrClouds with one or more collections.


## TODO:

Where to handle collections i the rule setup?

How to handle rows + grouping as that is different from rows alone?

## Sample

```
somesetup:
  solrShield:
    baseCost: 1.0
    maxAllowedCost: 100.0
    arguments:
      - key: "rows"
        default: 10
        rules: 
          - action: "setToDefault"
            lessThan: 0
            continue: true
          - action: "cancel"
            greaterThan: 1000
          - costMultiplier: 2.0
            greaterThan: 100
          - costMultiplier: 1.1
            greaterThan: 30
      - key: "group"
        rules: 
          - equals: true
            baseCostAddition: 10
      - key: "group.limit"
        default: 1
        rules:
          - action: "cancel"
            greaterThan: 1000
          - costMultiplier: 50
            greaterThan: 500
          - costMultiplier: 25
            greaterThan: 100
          - costMultiplier: 10
            greaterThan: 10
          - costMultiplier: 5
            greaterThan: 1            
          - equals: 1 # Grouping has optimizations for limit==1
            costMultiplier: 2
            
```
