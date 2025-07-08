# Word Clustering Application

A Java-based application that performs word clustering using Virtual Threads for parallel processing. Supports three clustering algorithms: Nearest Neighbor, K-Means, and Hierarchical. Designed for exploring semantic word relationships efficiently.

---

## Main Features

- Load and process word embeddings from a `.txt` file
- Search for similar words using 3 clustering algorithms
- Customize number of threads for performance tuning
- Save results to a file of your choice
- Color-coded console output and progress feedback
- Execution time logging for comparisons

---

## Clustering Methods

### 1. Nearest Neighbor
- Quickly finds the closest words by vector distance

### 2. K-Means Clustering
- Groups words into clusters by centroid similarity

### 3. Hierarchical Clustering
- Builds nested clusters to reveal complex relationships

---

## How to Use

1. Clone this repo or download the source code.
2. **Add your own `embeddings.txt` file** to the project root directory.  
   - *Note: This file is not included in the repository due to size limitations.*
3. Compile and run the project:
   ```bash
   javac -d bin src/ie/atu/sw/*.java
   java -cp bin ie.atu.sw.Main
