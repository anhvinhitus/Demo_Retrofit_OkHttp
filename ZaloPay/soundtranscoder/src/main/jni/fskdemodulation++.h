#ifndef FSK_DEMODULATIONPP_H
#define FSK_DEMODULATIONPP_H


struct FSKDemodulationConfig
{
    float dutyCycle;
    float f0;
    float f1;
    int bandWidth;
    int numTaps;
    float activeTime;
    float silenceTime;
    int samplingRate;
    int enjNSum;
    int maximumInterval;
    float thresholdEnj0;
    float thresholdEnj1;
    float binaryThreshold;
    int maxBitNumber;
    int header1PeakNumber;
};

enum FSKDemodulationStatus
{
    fskDemodulationStatusStart,
    fskDemodulationStatusFind1Peaks,
    fskDemodulationStatusFindFirst0Peak,
    fskDemodulationStatusFirst0PeakFound,
    fskDemodulationStatusEnd
};

class Filter;
class FSKDemodulationPrivate;

class FSKDemodulation
{
public:
    FSKDemodulation(FSKDemodulationConfig &fskDemoConfig);
    ~FSKDemodulation();
    char *output(int &textLength);
    FSKDemodulationStatus processOne(float sample);
    int reset();
private:
    FSKDemodulationPrivate *d;
};

#endif
