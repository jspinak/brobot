#!/bin/bash
# analyze_class.sh - Analyze a Java class for refactoring potential

CLASS_FILE=$1

if [ -z "$CLASS_FILE" ]; then
    echo "Usage: ./analyze_class.sh <path/to/Class.java>"
    exit 1
fi

echo "Analyzing $CLASS_FILE..."
echo "================================="

# Line count
LINES=$(wc -l < "$CLASS_FILE")
echo "Total lines: $LINES"

# Method count
METHODS=$(grep -c "public\|private\|protected" "$CLASS_FILE")
echo "Method count: $METHODS"

# Dependency count
IMPORTS=$(grep -c "^import" "$CLASS_FILE")
echo "Import count: $IMPORTS"

# Responsibility indicators
echo -e "\nPotential responsibilities:"
grep -E "Manager|Service|Controller|Handler|Processor" "$CLASS_FILE" | head -5

# Complexity indicators
echo -e "\nComplexity indicators:"
echo "- Nested if statements: $(grep -c "if.*{" "$CLASS_FILE")"
echo "- Try-catch blocks: $(grep -c "try.*{" "$CLASS_FILE")"
echo "- Loops: $(grep -c "for\|while" "$CLASS_FILE")"

# Field count
FIELDS=$(grep -E "private|protected|public" "$CLASS_FILE" | grep -v "(" | wc -l)
echo "- Fields: $FIELDS"

# Inner classes
INNER=$(grep -c "class.*{" "$CLASS_FILE")
echo "- Inner classes: $((INNER - 1))"

# Comments and documentation
COMMENTS=$(grep -c "//" "$CLASS_FILE")
JAVADOC=$(grep -c "/\*\*" "$CLASS_FILE")
echo -e "\nDocumentation:"
echo "- Line comments: $COMMENTS"
echo "- Javadoc blocks: $JAVADOC"