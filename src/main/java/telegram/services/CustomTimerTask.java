package telegram.services;


/**
 * Task to be execute periodically
 * @author Nikita Zinoviev
 */
public class CustomTimerTask {
    private String taskName = "";
    private int times = 1;

    public CustomTimerTask(String taskName, int times){
        this.taskName = taskName;
        this.times = times;
    }

    public String getTaskName(){
        return this.taskName;
    }

    public void setTaskName(String taskName){
        this.taskName = taskName;
    }

    /**
     *
     * @return times the tusk must be executed
     */
    public int getTimes(){
        return this.times;
    }

    /**
     *
     * @param times Number of times the tusk must be executed
     */
    public void setTimes(int times){
        this.times = times;
    }

    public void reduceTimes(){
        if (this.times > 0){
            this.times -= 1;
        }
    }

    public void execute(){}
}
