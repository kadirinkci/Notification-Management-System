(() => {
    "use strict";

    const REFRESH_INTERVAL_MS = 15_000;

    const refreshButton = document.getElementById("refresh-button");
    const refreshStatus = document.getElementById("refresh-status");

    const channelRows = document.getElementById("channel-rows");
    const hourlyRows = document.getElementById("hourly-rows");
    const failureRows = document.getElementById("failure-rows");

    const statElements = {
        total: document.getElementById("stat-total"),
        sent: document.getElementById("stat-sent"),
        failed: document.getElementById("stat-failed"),
        pending: document.getElementById("stat-pending"),
        retrying: document.getElementById("stat-retrying"),
        successRate: document.getElementById("stat-success-rate"),
        failureRate: document.getElementById("stat-failure-rate")
    };

    const numberFormatter = new Intl.NumberFormat("tr-TR");

    const dateTimeFormatter = new Intl.DateTimeFormat("tr-TR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"
    });

    const hourFormatter = new Intl.DateTimeFormat("tr-TR", {
        day: "2-digit",
        month: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });

    let isRefreshing = false;
    let hasLoadedData = false;

    function createCell(value) {
        const cell = document.createElement("td");
        cell.textContent = value;
        return cell;
    }

    function createBadgeCell(value, classPrefix) {
        const cell = document.createElement("td");
        const badge = document.createElement("span");

        badge.className = `${classPrefix} ${classPrefix}--${value}`;
        badge.textContent = value;

        cell.appendChild(badge);
        return cell;
    }

    function formatNumber(value) {
        return numberFormatter.format(Number(value ?? 0));
    }

    function formatPercentage(value) {
        return `${Number(value ?? 0).toLocaleString("tr-TR", {
            minimumFractionDigits: 1,
            maximumFractionDigits: 1
        })}%`;
    }

    function formatDateTime(value, formatter = dateTimeFormatter) {
        if (!value) {
            return "—";
        }

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value).replace("T", " ");
        }

        return formatter.format(date);
    }

    function setRefreshStatus(message, isError = false) {
        refreshStatus.textContent = message;
        refreshStatus.classList.toggle("refresh-status--error", isError);
    }

    function renderSummary(summary) {
        statElements.total.textContent = formatNumber(summary.total);
        statElements.sent.textContent = formatNumber(summary.sent);
        statElements.failed.textContent = formatNumber(summary.failed);
        statElements.pending.textContent = formatNumber(summary.pending);
        statElements.retrying.textContent = formatNumber(summary.retrying);
        statElements.successRate.textContent =
            formatPercentage(summary.successRate);
        statElements.failureRate.textContent =
            formatPercentage(summary.failureRate);
    }

    function renderChannels(channels) {
        channelRows.replaceChildren();

        if (!channels || channels.length === 0) {
            const row = document.createElement("tr");
            const cell = createCell("Kanal istatistiği bulunamadı.");

            cell.colSpan = 6;
            cell.className = "dashboard-empty-row";

            row.appendChild(cell);
            channelRows.appendChild(row);
            return;
        }

        channels.forEach(channel => {
            const row = document.createElement("tr");

            row.appendChild(createBadgeCell(channel.channel, "badge"));
            row.appendChild(createCell(formatNumber(channel.total)));
            row.appendChild(createCell(formatNumber(channel.sent)));
            row.appendChild(createCell(formatNumber(channel.failed)));
            row.appendChild(createCell(formatNumber(channel.pending)));
            row.appendChild(createCell(formatNumber(channel.retrying)));

            channelRows.appendChild(row);
        });
    }

    function createActivityCell(total, maximum) {
        const cell = document.createElement("td");
        const bar = document.createElement("div");
        const fill = document.createElement("span");

        const percentage = total === 0
            ? 0
            : Math.max((total / maximum) * 100, 4);

        bar.className = "activity-bar";
        bar.setAttribute("role", "progressbar");
        bar.setAttribute("aria-label", `${total} bildirim`);
        bar.setAttribute("aria-valuemin", "0");
        bar.setAttribute("aria-valuemax", String(maximum));
        bar.setAttribute("aria-valuenow", String(total));

        fill.className = "activity-bar__fill";
        fill.style.width = `${percentage}%`;

        bar.appendChild(fill);
        cell.appendChild(bar);

        return cell;
    }

    function renderHourly(hourly) {
        hourlyRows.replaceChildren();

        if (!hourly || hourly.length === 0) {
            const row = document.createElement("tr");
            const cell = createCell("Son 24 saate ait veri bulunamadı.");

            cell.colSpan = 7;
            cell.className = "dashboard-empty-row";

            row.appendChild(cell);
            hourlyRows.appendChild(row);
            return;
        }

        const maximum = Math.max(
            ...hourly.map(item => Number(item.total ?? 0)),
            1
        );

        hourly.forEach(item => {
            const row = document.createElement("tr");

            row.appendChild(
                createCell(formatDateTime(item.hour, hourFormatter))
            );
            row.appendChild(createCell(formatNumber(item.total)));
            row.appendChild(createCell(formatNumber(item.sent)));
            row.appendChild(createCell(formatNumber(item.failed)));
            row.appendChild(createCell(formatNumber(item.pending)));
            row.appendChild(createCell(formatNumber(item.retrying)));
            row.appendChild(
                createActivityCell(Number(item.total ?? 0), maximum)
            );

            hourlyRows.appendChild(row);
        });
    }

    function createNotificationLinkCell(notificationId) {
        const cell = document.createElement("td");
        const link = document.createElement("a");

        link.href = `/admin/notifications/${notificationId}`;
        link.textContent = `#${notificationId}`;

        cell.appendChild(link);
        return cell;
    }

    function renderFailures(failures) {
        failureRows.replaceChildren();

        if (!failures || failures.length === 0) {
            const row = document.createElement("tr");
            const cell = createCell("Yakın zamanda hata kaydı bulunamadı.");

            cell.colSpan = 6;
            cell.className = "dashboard-empty-row";

            row.appendChild(cell);
            failureRows.appendChild(row);
            return;
        }

        failures.forEach(failure => {
            const row = document.createElement("tr");

            row.appendChild(
                createNotificationLinkCell(failure.notificationId)
            );
            row.appendChild(createBadgeCell(failure.channel, "badge"));
            row.appendChild(
                createCell(formatNumber(failure.attemptNumber))
            );
            row.appendChild(
                createCell(formatDateTime(failure.attemptedAt))
            );
            row.appendChild(
                createBadgeCell(failure.outcome, "outcome-badge")
            );

            const reasonCell = createCell(
                failure.failureReason || "Açıklama yok"
            );
            reasonCell.className = "failure-reason";
            row.appendChild(reasonCell);

            failureRows.appendChild(row);
        });
    }

    function renderDashboard(data) {
        renderSummary(data.summary);
        renderChannels(data.channels);
        renderHourly(data.hourly);
        renderFailures(data.recentFailures);
    }

    async function refreshStats() {
        if (isRefreshing) {
            return;
        }

        isRefreshing = true;
        refreshButton.disabled = true;
        setRefreshStatus("İstatistikler yenileniyor…");

        try {
            const response = await fetch("/api/stats", {
                method: "GET",
                headers: {
                    Accept: "application/json"
                },
                cache: "no-store"
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const data = await response.json();

            renderDashboard(data);
            hasLoadedData = true;

            setRefreshStatus(
                `Son güncelleme: ${formatDateTime(data.generatedAt)}`
            );
        } catch (error) {
            console.error("Dashboard istatistikleri alınamadı:", error);

            setRefreshStatus(
                hasLoadedData
                    ? "İstatistikler yenilenemedi. Son başarılı veriler gösteriliyor."
                    : "İstatistikler yüklenemedi. Uygulama bağlantısını kontrol edin.",
                true
            );
        } finally {
            isRefreshing = false;
            refreshButton.disabled = false;
        }
    }

    refreshButton.addEventListener("click", refreshStats);

    refreshStats();
    window.setInterval(refreshStats, REFRESH_INTERVAL_MS);
})();
