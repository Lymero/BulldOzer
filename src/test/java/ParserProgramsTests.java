import norswap.autumn.AutumnTestFixture;
import org.testng.annotations.Test;

public class ParserProgramsTests extends AutumnTestFixture {

    GrammarParser parser = new GrammarParser();

//    public static ArrayList listFilesForFolder(File folder) {
//        ArrayList<File> files = new ArrayList<>();
//        for (final File fileEntry : folder.listFiles()) {
//            if (fileEntry.isDirectory()) {
//                listFilesForFolder(fileEntry);
//            } else {
//                files.add(fileEntry);
//            }
//        }
//        return files;
//    }
//
//    @Test
//    public void testRoot() {
//        this.rule = bulldOzerParser.root;
//        File dir = new File(".\\src\\test\\test_programs");
//        ArrayList<File> programs = listFilesForFolder(dir);
//        for (File program : programs) {
//            try {
//                String source = new String(Files.readAllBytes(Paths.get(program.getPath())));
//                System.out.println("Testing program : " + program.getPath());
//                success(source);
//            } catch (IOException e) {
//                System.err.println("Could not read test program content.");
//                e.printStackTrace();
//            }
//        }
//    }

    @Test
    public void testAdditionalFeatures() {
        this.rule = parser.root;
        success("# This program is used to show our additional features.\n" +
                "# It features the following features (explained in the document):\n" +
                "# - Default parameters\n" +
                "# - Named parameters\n" +
                "# - Type inference with the keyword \"var\"\n" +
                "# - For in loop\n" +
                "# - String interpolation\n" +
                "# - Tuple\n" +
                "# - String concatenation\n" +
                "# - Array concatenation\n" +
                "# - Functions as values\n" +
                "# - Nested functions\n" +
                "\n" +
                "# function that has a default parameter\n" +
                "func var look_up_value(array<integer> arr = [1, 2, 3, 4, 5, 6], integer to_find) {\n" +
                "    # \"var\" type inference, \"index\" is actually an integer\n" +
                "    var index = 0\n" +
                "    for (element : arr) { # for in loop\n" +
                "        if (element == to_find) {\n" +
                "            print(\"The element {element} has been found.\") # string interpolation\n" +
                "            ret index\n" +
                "        }\n" +
                "        index = index + 1\n" +
                "    }\n" +
                "    # type inference for the return of the function\n" +
                "    ret -1\n" +
                "}\n" +
                "\n" +
                "# named parameter + use the default parameter\n" +
                "integer first_found = look_up_value(to_find: 5)\n" +
                "\n" +
                "# \"var\" type inference\n" +
                "var second_found = look_up_value([10, 11, 12], 13)\n" +
                "\n" +
                "# tuple + type inference\n" +
                "var my_tuple = (first_found, second_found)\n" +
                "integer retrieved_first_index = my_tuple[0]\n" +
                "# my_tuple[0] = 1 # not possible, tuples are immutable\n" +
                "\n" +
                "var concatenation_array = [1, 2, 3, 4] + [5, 6] + [7, 8, 9, 10]\n" +
                "array<integer> concatenation_array_bis = [1, 2, 3, 4] + [5, 6] + [7, 8, 9, 10]\n" +
                "# type of the elements inside the array can be inferred too\n" +
                "integer x = concatenation_array[0]\n" +
                "string string_concatenation = \"Hello\" + \" \" + \"world.\"\n" +
                "\n" +
                "# returns a function using the var keyword to infer the type of the function\n" +
                "func var create_function_string_concat(string s) {\n" +
                "    func string own_string_concat(integer x = 1337) {\n" +
                "        ret s + \"{x}\"\n" +
                "    }\n" +
                "    ret own_string_concat\n" +
                "}\n" +
                "\n" +
                "var my_string_concat = create_function_string_concat(\"Super string -> \")\n" +
                "string s = my_string_concat(5)\n" +
                "string s = my_string_concat() # use the default parameter");
    }


    @Test
    public void testFibonacci() {
        this.rule = parser.root;
        success("func void fibonacci(integer a, integer b, integer N) {\n" +
                "    if (N == 0) { ret }\n" +
                "    print(a)\n" +
                "    fibonacci(b, a+b, N-1)\n" +
                "}\n" +
                "\n" +
                "integer N = parseToInt($1)\n" +
                "fibonacci(0, 1, N)\n");
    }

    @Test
    public void testFizzBuzz() {
        this.rule = parser.root;
        success("integer i = 1\n" +
                "while (i <= 100) {\n" +
                "    if (i % 15 == 0) {\n" +
                "        print(\"FizzBuzz\")\n" +
                "    }\n" +
                "    elif (i % 3 == 0) {\n" +
                "        print(\"Fizz\")\n" +
                "    }\n" +
                "    elif (i % 5 == 0) {\n" +
                "            print(\"Buzz\")\n" +
                "    }\n" +
                "    else {\n" +
                "        print(i)\n" +
                "    }\n" +
                "    i = i + 1\n" +
                "}\n");
    }

    @Test
    public void testPrime() {
        this.rule = parser.root;
        success("func bool isPrime(integer number) {\n" +
                "    if (number <= 1) {\n" +
                "        ret false\n" +
                "    }\n" +
                "    bool prime = true\n" +
                "    integer i = 2\n" +
                "    while (i < number && prime) {\n" +
                "        if (number % i == 0) {\n" +
                "            prime = false\n" +
                "        }\n" +
                "        i = i + 1\n" +
                "    }\n" +
                "    ret prime\n" +
                "}\n" +
                "\n" +
                "# MAIN\n" +
                "integer N = parseToInt($0)\n" +
                "integer current = 2\n" +
                "integer count = 0\n" +
                "\n" +
                "while (count < N) {\n" +
                "    if (isPrime(current)) {\n" +
                "        print(current)\n" +
                "        count = count + 1\n" +
                "    }\n" +
                "    current = current + 1\n" +
                "}\n");
    }

    @Test
    public void testSort() {
        this.rule = parser.root;
        success("func void swap(array<integer> a, integer i, integer j) {\n" +
                "    integer tmp = a[i]\n" +
                "    a[i] = a[j]\n" +
                "    a[j] = tmp\n" +
                "}\n" +
                "\n" +
                "func void sort(array<integer> numbers) {\n" +
                "    integer i = 0\n" +
                "    while (i < len(numbers)) {\n" +
                "        integer j = i+1\n" +
                "        while (j < len(numbers)) {\n" +
                "            if (numbers[i] > numbers[j]) {\n" +
                "                swap(numbers, i, j)\n" +
                "            }\n" +
                "            j = j + 1\n" +
                "        }\n" +
                "        i = i + 1\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "# MAIN\n" +
                "array<integer> numbers = []\n" +
                "integer i = 0\n" +
                "while (i < $@) {\n" +
                "    numbers[i] = parseToInt($i)\n" +
                "    i = i + 1\n" +
                "}\n" +
                "sort(numbers)\n" +
                "i = 0\n" +
                "while (i < len(numbers)) {\n" +
                "    print(parseToString(numbers[i]))\n" +
                "    i = i + 1\n" +
                "}\n");
    }

    @Test
    public void testUniq() {
        this.rule = parser.root;
        success("map<string, bool> m = {}\n" +
                "integer i = 0\n" +
                "while (i < $@) {\n" +
                "    if (m[$i] == unknown) {\n" +
                "        print($i)\n" +
                "        m[$i] = true\n" +
                "    }\n" +
                "    i = i + 1\n" +
                "}\n");
    }
}
