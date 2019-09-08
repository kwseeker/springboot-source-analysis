package top.kwseeker.spring.configuration.commandline;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

public class DefaultApplicationArgumentsTest {

    // nonOptionArg=test --author=ArvinLee --mail=xiaohuileee@gmail.com
    public static void main(String[] args) {
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(
                args);

        String[] argsStr = applicationArguments.getSourceArgs();
        for (String str : argsStr) {
            System.out.println(str);
        }
        System.out.println(applicationArguments.getOptionValues("author"));
        applicationArguments.getNonOptionArgs().forEach(v -> System.out.println(v));
        applicationArguments.getOptionNames().forEach(v -> System.out.println(v));

        //解析流程
        //public CommandLineArgs parse(String... args) {
        //    CommandLineArgs commandLineArgs = new CommandLineArgs();
        //    for (String arg : args) {
        //        if (arg.startsWith("--")) {
        //            String optionText = arg.substring(2, arg.length());
        //            String optionName;
        //            String optionValue = null;
        //            if (optionText.contains("=")) {
        //                optionName = optionText.substring(0, optionText.indexOf('='));
        //                optionValue = optionText.substring(optionText.indexOf('=')+1, optionText.length());
        //            }
        //            else {
        //                optionName = optionText;
        //            }
        //            if (optionName.isEmpty() || (optionValue != null && optionValue.isEmpty())) {
        //                throw new IllegalArgumentException("Invalid argument syntax: " + arg);
        //            }
        //            commandLineArgs.addOptionArg(optionName, optionValue);
        //        }
        //        else {
        //            commandLineArgs.addNonOptionArg(arg);
        //        }
        //    }
        //    return commandLineArgs;
        //}
    }
}
