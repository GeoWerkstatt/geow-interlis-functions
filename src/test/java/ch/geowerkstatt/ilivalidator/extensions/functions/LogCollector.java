package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.logging.LogEvent;
import ch.interlis.iox.IoxLogEvent;

import java.util.ArrayList;


public class LogCollector implements ch.interlis.iox.IoxLogging {
    private ArrayList<IoxLogEvent> errs=new ArrayList<IoxLogEvent>();
    private ArrayList<IoxLogEvent> warn=new ArrayList<IoxLogEvent>();

    @Override
    public void addEvent(IoxLogEvent event) {
        EhiLogger.getInstance().logEvent((LogEvent) event);
        if(event.getEventKind()==IoxLogEvent.ERROR){
            errs.add(event);
        }else if(event.getEventKind()==IoxLogEvent.WARNING){
            warn.add(event);
        }
    }

    public ArrayList<IoxLogEvent> getErrs() {
        return errs;
    }

    public ArrayList<IoxLogEvent> getWarn() {
        return warn;
    }
}
