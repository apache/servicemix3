import java.util.Map;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 11, 2007
 * Time: 3:03:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] {"../resources/META-INF/spring/gshell.xml", "../resources/META-INF/spring/gshell-local.xml", "../resources/META-INF/spring/gshell-commands.xml"});

        CommandRegistry registry = (CommandRegistry) context.getBean("commandRegistry");
        Map<String, Command> commands = (Map<String, Command>) context.getBeansOfType(Command.class);
        for (Command cmd : commands.values()) {
            registry.register(cmd);
        }

    }
}
