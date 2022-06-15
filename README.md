# KSP Java Inner class Bug Example

This repository has example of reported bug https://github.com/google/ksp/issues/1022

## Summary

KSVisitor.visitPropertyDeclaration can't find property with a type contained as an inner class

## Workaround

Use KSClassDeclaration.getAllProperties() to get all property

