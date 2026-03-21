import React from "react";
import {LoaderCircle} from "lucide-react";

function CreditsValue({credits, isLoading, loadingLabel = "", emptyLabel = "--"}) {
    if (isLoading) {
        return (
            <span className="inline-flex items-center gap-2">
                <LoaderCircle className="h-4 w-4 animate-spin" />
                <span>{loadingLabel}</span>
            </span>
        );
    }

    if (credits == null) {
        return <span>{emptyLabel}</span>;
    }

    return <span>{credits}</span>;
}

export default CreditsValue;
