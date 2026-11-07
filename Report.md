# Assignment 4 Report
## Dynamic Programming and String Algorithms

## 1. Experiment Design Card

### Question

**Edit Distance:**

How does the number of operations change when the input strings become longer? I also want to compare the three versions of Edit Distance.

**String Search:**

How do Naive Search and KMP perform on random and worst-case inputs?

### Sizes

**Edit Distance:**

- Recursive: 2, 4, 6, 8
- Table: 100, 200, 400, 800, 1600
- Two Rows: 100, 200, 400, 800, 1600

**String Search:**

- 1000
- 2000
- 4000
- 8000
- 16000

I chose smaller sizes for Recursive Edit Distance because it becomes very slow when the input grows. The other algorithms can handle larger inputs.

### Inputs

**Edit Distance:**

1. Random strings: generated using the alphabet ABCD.
2. All mismatch: the first string contains only A characters, and the second contains only B characters.

**String Search:**

1. Random: random text and random pattern.
2. Worst case: the text contains only A characters, while the pattern contains A characters and ends with B.

These inputs help me compare normal cases and difficult cases.

### Metrics

The experiment records:

- Number of operations.
- Runtime in milliseconds.
- Median runtime of three runs.

For Recursive Edit Distance, one operation means one recursive call.

For Table and Two Rows, one operation means one calculated DP cell.

For Naive Search, one operation means one character comparison.

For KMP, operations include comparisons during LPS construction and searching.

### Prediction

I expected Recursive Edit Distance to grow exponentially.

I expected Table and Two Rows to perform O(n*m) operations because both calculate the same DP cells.

I also expected Naive Search to perform many comparisons on the worst-case input. KMP should be faster because it uses the LPS array.

### Plots

I planned four plots:

1. Recursive Exponential Growth - shows how recursive calls increase with input size.
2. Edit Distance DP Operations - compares the number of calculated cells in Table and Two Rows.
3. String Search Worst Case - compares Naive and KMP operation counts.
4. String Search Runtime - compares their median execution times.

The Recursive, String Search Worst Case and String Search Runtime plots use a logarithmic Y-axis. The DP plot uses a linear Y-axis.

---

## 2. Results and Plots

### Plot 1: Recursive Exponential Growth

The number of recursive calls grows very quickly when the input size increases. At n = 8, the algorithm already makes 398,593 calls.

### Plot 2: Edit Distance DP Operations

Table and Two Rows calculate the same number of DP cells. However, Two Rows uses less memory because it stores only two rows.

### Plot 3: String Search Worst Case

Naive Search makes many more character comparisons than KMP. The difference becomes much larger when the text gets longer.

### Plot 4: String Search Runtime
Naive Search takes more time on larger worst-case inputs. KMP is much faster in this experiment, although its measured time does not increase evenly.

---

## 3. Prediction vs Result

### Edit Distance

My prediction was that Recursive Edit Distance would grow exponentially. The results support this prediction.

In the all-mismatch experiment, it made 19 calls at n = 2, 481 calls at n = 4, 13,483 calls at n = 6, and 398,593 calls at n = 8.

In this experiment, random and all-mismatch inputs produced the same operation counts for the tested sizes. This is what I observed in the CSV results.

Table and Two Rows had the same operation counts. At n = 1600, both calculated 2,560,000 DP cells. This agrees with the expected O(n*m) time complexity.

The main difference is memory usage. Table stores the full DP table, while Two Rows stores only two arrays.

### String Search

My prediction was that Naive Search would perform more comparisons than KMP on the worst-case input. The results clearly support this prediction.

At n = 1000, Naive Search made 250,500 comparisons, while KMP made 2,498 comparisons.

At n = 16000, Naive Search made 64,008,000 comparisons, while KMP made only 39,998 comparisons.

This shows that KMP is much more efficient for this type of input.

---

## 4. Short Answers

### 1. Which input made your string search slowest, and why does it do that?

The worst-case input made Naive Search the slowest. The text contains only A characters, while the pattern contains A characters and ends with B. At almost every starting position, Naive Search compares many characters before finding a mismatch. Because of this, it performs a very large number of comparisons.

### 2. How large an n could the recursive Edit Distance handle, and what stopped it?

In my experiment, I tested Recursive Edit Distance up to n = 8. At this size, it already made 398,593 recursive calls. The number of calls grows very quickly, so I did not test larger sizes. The main problem was the large amount of repeated work.

### 3. What do you gain and what do you lose when you use two rows instead of the full table?

Two Rows uses less memory than the full DP table. Its space complexity is O(m), while the full table uses O(n*m) space. Both algorithms calculate the same number of DP cells and return the same answer. However, Two Rows does not keep all previous DP values, so we cannot access the complete table later.

### 4. If you had to choose one string search for your city platform, which one and why?

I would choose KMP for the city search platform. It works efficiently on long texts because its time complexity is O(n+m). It also finds overlapping matches. Our experiment showed that KMP makes much fewer comparisons than Naive Search on the worst-case input. This makes it a better choice for searching city names and long log files.

---

## 5. Time Measurements

I measured each input three times and used the median runtime.

The results show that larger inputs usually require more time, especially for Recursive Edit Distance and Naive Search on worst-case inputs.

For example, Naive Search took a median of 6.4698 ms at n = 1000 and 141.1233 ms at n = 16000. KMP took 0.3185 ms at n = 1000 and 0.1025 ms at n = 16000.

The KMP runtime did not increase evenly in the last measurements. This can happen because execution time depends on the computer, Java optimization and other running processes.

Therefore, I did not use runtime as my only way to compare algorithms. Operation counters give a more stable comparison because they show how much work each algorithm performs.

---

## 6. Conclusion

In this assignment, I implemented three versions of Edit Distance and two String Search algorithms.

The experiment showed that Recursive Edit Distance becomes very expensive as the input grows. Table and Two Rows perform the same number of DP calculations, but Two Rows needs less memory.

I also saw a big difference between Naive Search and KMP on the worst-case input. KMP performed far fewer comparisons and was a better choice for large text searches.

Overall, this assignment helped me understand how algorithm design affects execution time, operation counts and memory usage.

---