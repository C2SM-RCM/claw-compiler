/*
 * @author Mikhail Zhigun
 * @copyright Copyright 2020, MeteoSwiss
 */
package clawfc.depscan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import clawfc.depscan.serial.FortranStatementPosition;

public class FortranModuleInfo
{
    clawfc.depscan.serial.FortranModuleInfo _data;

    public clawfc.depscan.serial.FortranModuleInfo data()
    {
        return _data;
    }

    public String getName()
    {
        return _data.getInfo().getName();
    }

    public long getStartLineIdx()
    {
        return _data.getInfo().getStartLineIdx();
    }

    public long getEndLineIdx()
    {
        return _data.getInfo().getEndLineIdx();
    }

    public long getStartCharIdx()
    {
        return data().getInfo().getStartCharIdx();
    }

    public long getEndCharIdx()
    {
        return data().getInfo().getEndCharIdx();
    }

    public List<String> getUsedModuleNames()
    {
        List<String> res = new ArrayList<String>();
        for (FortranStatementPosition pos : _data.getUsedModules().getUse())
        {
            res.add(pos.getName());
        }
        return Collections.unmodifiableList(res);
    }

    public boolean getUsesClaw()
    {
        return data().isUsesClaw();
    }

    public FortranModuleInfo(clawfc.depscan.serial.FortranModuleInfo data)
    {
        _data = data;
    }

    public FortranModuleInfo(clawfc.depscan.FortranStatementPosition pos,
            List<clawfc.depscan.FortranStatementPosition> useModules, boolean usesClaw)
    {
        _data = new clawfc.depscan.serial.FortranModuleInfo();
        _data.setInfo(pos.getData());
        _data.setUsedModules(new clawfc.depscan.serial.FortranModuleInfo.UsedModules());
        for (clawfc.depscan.FortranStatementPosition useModPos : useModules)
        {
            _data.getUsedModules().getUse().add(useModPos.getData());
        }
        _data.setUsesClaw(usesClaw);
    }

    /*
     * public FortranModuleInfo(FortranModuleBasicInfo info, long startCharPos, long
     * endCharPos, boolean usesCLAW) { _data = new
     * clawfc.depscan.serial.FortranModuleInfo();
     * clawfc.depscan.FortranModuleBasicInfo.assign(data(), info.data());
     * data().setStartCharPos(startCharPos); data().setEndCharPos(endCharPos);
     * data().setUsesClaw(usesCLAW); }
     * 
     * public FortranModuleInfo(String name, long startLineNum, long endLineNum,
     * Collection<String> usedModuleNames, long startCharPos, long endCharPos,
     * boolean usesCLAW) { _data = new clawfc.depscan.serial.FortranModuleInfo();
     * clawfc.depscan.FortranModuleBasicInfo.assign(data(), name, startLineNum,
     * endLineNum, usedModuleNames); data().setStartCharPos(startCharPos);
     * data().setEndCharPos(endCharPos); data().setUsesClaw(usesCLAW); }
     */

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        FortranModuleInfo other = (FortranModuleInfo) obj;
        if (!clawfc.depscan.FortranStatementPosition.equals(_data.getInfo(), other._data.getInfo()))
        {
            return false;
        }
        if (_data.getUsedModules().getUse().size() != other._data.getUsedModules().getUse().size())
        {
            return false;
        }
        for (int i = 0, n = _data.getUsedModules().getUse().size(); i < n; ++i)
        {
            clawfc.depscan.serial.FortranStatementPosition p = _data.getUsedModules().getUse().get(i);
            clawfc.depscan.serial.FortranStatementPosition otherP = other._data.getUsedModules().getUse().get(i);
            if (!clawfc.depscan.FortranStatementPosition.equals(p, otherP))
            {
                return false;
            }
        }
        if (getStartCharIdx() != other.getStartCharIdx())
        {
            return false;
        }
        if (getEndCharIdx() != other.getEndCharIdx())
        {
            return false;
        }
        if (getUsesClaw() != other.getUsesClaw())
        {
            return false;
        }
        return true;
    }
}
