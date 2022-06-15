# KSP Java Inner class Bug Example

This repository has example of reported bug

## Summary

KSVisitor.visitPropertyDeclaration can't find property with a type contained as an inner class

## Workaround

Use KSClassDeclaration.getAllProperties() to get all property

